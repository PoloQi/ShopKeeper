import { useEffect, useState } from 'react'
import {
  Table,
  Button,
  Form,
  Input,
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
import * as supplierApi from '../api/supplierApi'
import { phonePattern } from '../utils/validators'
import { useAuth } from '../context/AuthContext'

const emptyQuery = { supplierName: '', phone: '', status: undefined }

export default function Supplier() {
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
  const { user } = useAuth()

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await supplierApi.page({ ...applied, page, size })
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
    // 提交前去掉首尾空格，与后端归一一致
    if (values.phone) values.phone = values.phone.trim()
    setSaving(true)
    try {
      if (editing) {
        await supplierApi.update({ ...values, supplierId: editing.supplierId })
      } else {
        await supplierApi.add(values)
      }
      message.success('保存成功')
      setModalOpen(false)
      loadData()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (supplierId) => {
    await supplierApi.remove(supplierId)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '供应商编号', dataIndex: 'supplierId', width: 110 },
    { title: '供应商名称', dataIndex: 'supplierName' },
    { title: '联系人', dataIndex: 'contactPerson', width: 100 },
    { title: '联系电话', dataIndex: 'phone', width: 140 },
    { title: '地址', dataIndex: 'address' },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (s) => (s === 1 ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>)
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      render: (_, record) => (
        <Space size="middle">
          <a onClick={() => openEdit(record)}>编辑</a>
          {user.role === 1 ? (
            <Popconfirm
              title="删除确认"
              description={`确定删除供应商「${record.supplierName}」吗？`}
              okText="确定删除"
              cancelText="取消"
              okButtonProps={{ danger: true }}
              onConfirm={() => handleDelete(record.supplierId)}
            >
              <a style={{ color: 'var(--cinnabar)' }}>删除</a>
            </Popconfirm>
          ) : (
            <span style={{ color: '#bbb' }}>删除</span>
          )}
        </Space>
      )
    }
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }} styles={{ body: { paddingBottom: 0 } }}>
        <Row gutter={16}>
          <Col span={6}>
            <Form.Item label="供应商名称">
              <Input
                allowClear
                placeholder="请输入供应商名称"
                value={query.supplierName}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, supplierName: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item label="联系电话">
              <Input
                allowClear
                placeholder="请输入联系电话"
                value={query.phone}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, phone: e.target.value })}
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
                  { value: 1, label: '启用' },
                  { value: 0, label: '停用' }
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
        title="供应商列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
            新增供应商
          </Button>
        }
      >
        <Table
          rowKey="supplierId"
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
        title={editing ? '编辑供应商' : '新增供应商'}
        open={modalOpen}
        width={520}
        destroyOnClose
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          {editing && (
            <Form.Item label="供应商编号">
              <Input value={editing.supplierId} disabled />
            </Form.Item>
          )}
          <Form.Item
            name="supplierName"
            label="供应商名称"
            rules={[{ required: true, message: '请输入供应商名称' }]}
          >
            <Input placeholder="请输入供应商名称" maxLength={100} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="contactPerson" label="联系人">
                <Input maxLength={50} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="phone"
                label="联系电话"
                rules={[
                  {
                    pattern: phonePattern,
                    transform: (v) => (typeof v === 'string' ? v.trim() : v),
                    message: '请输入11位手机号或带区号的固话'
                  }
                ]}
              >
                <Input maxLength={20} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="address" label="地址">
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 1, label: '启用' },
                { value: 0, label: '停用' }
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
