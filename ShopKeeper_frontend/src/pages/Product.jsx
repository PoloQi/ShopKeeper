import { useEffect, useState } from 'react'
import {
  Table,
  Button,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Card,
  Row,
  Col,
  Modal,
  message,
  Popconfirm,
  Tag
} from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import * as productApi from '../api/productApi'

const categoryOptions = [
  { value: '食品', label: '食品' },
  { value: '日用品', label: '日用品' },
  { value: '电器', label: '电器' }
]

const unitOptions = ['件', '箱', '个', '台', '包', '瓶', '桶', '袋', '提', '本'].map((u) => ({
  value: u,
  label: u
}))

const emptyQuery = { productName: '', category: undefined, status: undefined }

export default function Product() {
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [query, setQuery] = useState(emptyQuery)
  const [applied, setApplied] = useState(emptyQuery)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await productApi.page({ ...applied, page, size })
      setData(res.data.records)
      setTotal(res.data.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, applied])

  const handleSearch = () => {
    if (page !== 1) setPage(1)
    setApplied({ ...query })
  }

  const handleReset = () => {
    setQuery(emptyQuery)
    if (page !== 1) setPage(1)
    setApplied(emptyQuery)
  }

  const openAdd = () => {
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ status: 1 })
    setModalOpen(true)
  }

  const openEdit = (record) => {
    setEditing(record)
    form.setFieldsValue(record)
    setModalOpen(true)
  }

  const handleOk = async () => {
    const values = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await productApi.update({ ...values, productId: editing.productId })
      } else {
        await productApi.add(values)
      }
      message.success('保存成功')
      setModalOpen(false)
      loadData()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (productId) => {
    await productApi.remove(productId)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '商品编号', dataIndex: 'productId', width: 100 },
    { title: '商品名称', dataIndex: 'productName' },
    { title: '分类', dataIndex: 'category', width: 90 },
    { title: '规格型号', dataIndex: 'spec', width: 130 },
    { title: '单位', dataIndex: 'unit', width: 70 },
    {
      title: '单价(元)',
      dataIndex: 'unitPrice',
      width: 100,
      align: 'right'
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (s) => (s === 1 ? <Tag color="green">在售</Tag> : <Tag>停售</Tag>)
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Space size="middle">
          <a onClick={() => openEdit(record)}>编辑</a>
          <Popconfirm
            title="删除确认"
            description={`确定删除商品「${record.productName}」吗？`}
            okText="确定删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.productId)}
          >
            <a style={{ color: 'var(--cinnabar)' }}>删除</a>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }} styles={{ body: { paddingBottom: 0 } }}>
        <Row gutter={16}>
          <Col span={6}>
            <Form.Item label="商品名称">
              <Input
                allowClear
                placeholder="请输入商品名称"
                value={query.productName}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, productName: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item label="分类">
              <Select
                allowClear
                placeholder="全部分类"
                value={query.category}
                onChange={(v) => setQuery({ ...query, category: v })}
                options={categoryOptions}
              />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item label="状态">
              <Select
                allowClear
                placeholder="全部"
                value={query.status}
                onChange={(v) => setQuery({ ...query, status: v })}
                options={[
                  { value: 1, label: '在售' },
                  { value: 0, label: '停售' }
                ]}
              />
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item label=" ">
              <Space>
                <Button type="primary" onClick={handleSearch}>
                  查询
                </Button>
                <Button icon={<ReloadOutlined />} onClick={handleReset}>
                  重置
                </Button>
              </Space>
            </Form.Item>
          </Col>
        </Row>
      </Card>

      <Card
        title="商品列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
            新增商品
          </Button>
        }
      >
        <Table
          rowKey="productId"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1000 }}
          pagination={{
            current: page,
            pageSize: size,
            total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, s) => {
              setPage(p)
              setSize(s)
            }
          }}
        />
      </Card>

      <Modal
        title={editing ? '编辑商品' : '新增商品'}
        open={modalOpen}
        width={560}
        destroyOnClose
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          {editing && (
            <Form.Item label="商品编号">
              <Input value={editing.productId} disabled />
            </Form.Item>
          )}
          <Form.Item
            name="productName"
            label="商品名称"
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="请输入商品名称" maxLength={100} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="category" label="分类">
                <Select allowClear placeholder="请选择分类" options={categoryOptions} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="unit"
                label="计量单位"
                rules={[{ required: true, message: '请选择计量单位' }]}
              >
                <Select placeholder="请选择单位" options={unitOptions} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="spec" label="规格型号">
            <Input maxLength={50} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="unitPrice"
                label="标准单价(元)"
                rules={[{ required: true, message: '请输入单价' }]}
              >
                <InputNumber min={0} precision={2} step={0.1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态" rules={[{ required: true }]}>
                <Select
                  options={[
                    { value: 1, label: '在售' },
                    { value: 0, label: '停售' }
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  )
}
