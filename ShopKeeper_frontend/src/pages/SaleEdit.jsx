import { useEffect, useState } from 'react'
import {
  Card,
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Button,
  Space,
  Table,
  Row,
  Col,
  Spin,
  message
} from 'antd'
import { PlusOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import dayjs from 'dayjs'
import * as saleApi from '../api/saleApi'
import * as customerApi from '../api/customerApi'
import * as productApi from '../api/productApi'
import { useAuth } from '../context/AuthContext'

const discountOptions = [1, 0.95, 0.9, 0.85, 0.8, 0.7].map((d) => ({
  value: d,
  label: d === 1 ? '不打折' : `${d * 10}折`
}))

const newRow = () => ({ productId: undefined, quantity: 1, unitPrice: 0, discount: 1 })

const rowAmount = (r) => (r.unitPrice || 0) * (r.quantity || 0) * (r.discount ?? 1)

export default function SaleEdit() {
  const navigate = useNavigate()
  const location = useLocation()
  const { soNo } = useParams()
  const { user: authUser } = useAuth()

  // edit 可编辑；audit 只读且店长可审核；view 纯只读
  const mode = location.pathname.includes('/audit/')
    ? 'audit'
    : location.pathname.includes('/view/')
      ? 'view'
      : 'edit'
  const readOnly = mode !== 'edit'
  const hasNo = Boolean(soNo)

  const [form] = Form.useForm()
  const [loading, setLoading] = useState(hasNo)
  const [saving, setSaving] = useState(false)
  const [auditing, setAuditing] = useState(false)
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [items, setItems] = useState(() => [newRow()])

  useEffect(() => {
    customerApi.options().then((res) => setCustomers(res.data))
    productApi.options().then((res) => setProducts(res.data))
  }, [])

  useEffect(() => {
    if (!hasNo) return
    saleApi
      .getDetail(soNo)
      .then((res) => {
        const d = res.data
        form.setFieldsValue({
          ...d,
          orderDate: d.orderDate ? dayjs(d.orderDate) : null
        })
        setItems(
          d.items.map((it) => ({
            productId: it.productId,
            quantity: it.quantity,
            unitPrice: it.unitPrice,
            discount: it.discount
          }))
        )
      })
      .finally(() => setLoading(false))
  }, [hasNo, soNo, form])

  const updateRow = (index, patch) => {
    setItems(items.map((r, i) => (i === index ? { ...r, ...patch } : r)))
  }

  /** 选择商品：自动带出商品标准单价 */
  const handleProductChange = (index, productId) => {
    const p = products.find((x) => x.productId === productId)
    updateRow(index, { productId, unitPrice: p ? p.unitPrice : 0 })
  }

  const handleSave = async () => {
    const values = await form.validateFields()
    if (items.length === 0) {
      message.warning('请至少添加一条销售明细')
      return
    }
    const chosen = items.filter((r) => r.productId)
    if (chosen.length !== items.length) {
      message.warning('存在未选择商品的明细行')
      return
    }
    if (new Set(chosen.map((r) => r.productId)).size !== chosen.length) {
      message.warning('同一商品不能重复出现在明细中')
      return
    }

    const payload = {
      soNo: hasNo ? soNo : undefined,
      customerId: values.customerId,
      orderDate: values.orderDate.format('YYYY-MM-DD'),
      deliveryPlace: values.deliveryPlace,
      remark: values.remark,
      items: chosen
    }
    setSaving(true)
    try {
      await saleApi.save(payload)
      message.success('保存成功')
      navigate('/sale')
    } finally {
      setSaving(false)
    }
  }

  const handleAudit = async () => {
    setAuditing(true)
    try {
      await saleApi.audit(soNo)
      message.success('审核成功')
      navigate('/sale')
    } finally {
      setAuditing(false)
    }
  }

  const totalAmount = items.reduce((sum, r) => sum + rowAmount(r), 0)

  const columns = [
    {
      title: '商品',
      width: 260,
      render: (_, __, index) => (
        <Select
          showSearch
          disabled={readOnly}
          optionFilterProp="label"
          style={{ width: '100%' }}
          placeholder="请选择商品"
          value={items[index].productId}
          onChange={(v) => handleProductChange(index, v)}
          options={products.map((p) => ({
            value: p.productId,
            label: `${p.productId} ${p.productName}`
          }))}
        />
      )
    },
    {
      title: '数量',
      width: 110,
      render: (_, __, index) => (
        <InputNumber
          min={1}
          precision={0}
          disabled={readOnly}
          style={{ width: '100%' }}
          value={items[index].quantity}
          onChange={(v) => updateRow(index, { quantity: v })}
        />
      )
    },
    {
      title: '单价(元)',
      width: 130,
      render: (_, __, index) => (
        <InputNumber
          min={0}
          precision={2}
          disabled={readOnly}
          style={{ width: '100%' }}
          value={items[index].unitPrice}
          onChange={(v) => updateRow(index, { unitPrice: v })}
        />
      )
    },
    {
      title: '折扣',
      width: 110,
      render: (_, __, index) => (
        <Select
          style={{ width: '100%' }}
          disabled={readOnly}
          value={items[index].discount}
          onChange={(v) => updateRow(index, { discount: v })}
          options={discountOptions}
        />
      )
    },
    {
      title: '金额(元)',
      width: 120,
      align: 'right',
      render: (_, __, index) => rowAmount(items[index]).toFixed(2)
    },
  ]

  // 只读模式下不提供删行入口
  if (!readOnly) {
    columns.push({
      title: '操作',
      width: 80,
      render: (_, __, index) => (
        <a
          style={{ color: '#ff4d4f' }}
          onClick={() => setItems(items.filter((_, i) => i !== index))}
        >
          删除
        </a>
      )
    })
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <Card>
        <Form form={form} layout="vertical" disabled={readOnly}>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item label="销售单号">
                <Input value={hasNo ? soNo : '保存后自动生成'} disabled />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="customerId"
                label="客户"
                rules={[{ required: true, message: '请选择客户' }]}
              >
                <Select
                  showSearch
                  optionFilterProp="label"
                  placeholder="请选择客户"
                  options={customers.map((c) => ({
                    value: c.customerId,
                    label: `${c.customerId} ${c.customerName}`
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="orderDate"
                label="单据日期"
                initialValue={dayjs()}
                rules={[{ required: true, message: '请选择单据日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="deliveryPlace" label="交货地点">
                <Input maxLength={100} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="remark" label="备注" style={{ marginBottom: 0 }}>
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
        </Form>
      </Card>

      <Card
        title="销售明细"
        extra={
          readOnly
            ? null
            : (
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setItems([...items, newRow()])}
              >
                添加明细
              </Button>
            )
        }
      >
        <Table
          rowKey={(_, index) => index}
          columns={columns}
          dataSource={items}
          pagination={false}
          scroll={{ x: 900 }}
        />
        <div style={{ textAlign: 'right', marginTop: 16, fontSize: 16 }}>
          合计金额：<span style={{ color: '#f5222d', fontWeight: 600 }}>¥{totalAmount.toFixed(2)}</span>
        </div>
      </Card>

      <div>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/sale')}>
            返回
          </Button>
          {mode === 'edit' && (
            <Button type="primary" loading={saving} onClick={handleSave}>
              保存单据
            </Button>
          )}
          {mode === 'audit' && authUser?.role === 1 && (
            <Button type="primary" loading={auditing} onClick={handleAudit}>
              审核通过
            </Button>
          )}
        </Space>
      </div>
    </Space>
  )
}
