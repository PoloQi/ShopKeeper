import { useEffect, useState } from 'react'
import {
  Table,
  Button,
  Form,
  Input,
  Radio,
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
import * as userApi from '../api/userApi'
import { useAuth } from '../context/AuthContext'

const emptyQuery = { username: '', realName: '', status: undefined }

export default function User() {
  const { user: loginUser } = useAuth()
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
      const res = await userApi.page({ ...applied, page, size })
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
    form.setFieldsValue({ gender: 'M', status: 1 })
    setModalOpen(true)
  }

  const openEdit = (record) => {
    setEditing(record)
    form.setFieldsValue({ ...record, password: '' })
    setModalOpen(true)
  }

  const handleOk = async () => {
    const values = await form.validateFields()
    // 编辑时密码留空则不提交该字段
    if (editing && !values.password) {
      delete values.password
    }
    setSaving(true)
    try {
      if (editing) {
        await userApi.update({ ...values, userId: editing.userId })
      } else {
        await userApi.add(values)
      }
      message.success('保存成功')
      setModalOpen(false)
      loadData()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (userId) => {
    await userApi.remove(userId)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '用户编号', dataIndex: 'userId', width: 100 },
    { title: '用户名', dataIndex: 'username', width: 140 },
    { title: '真实姓名', dataIndex: 'realName', width: 140 },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 80,
      render: (g) => (g === 'M' ? '男' : g === 'F' ? '女' : '—')
    },
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
          {record.userId === loginUser.userId ? (
            <span style={{ color: '#bbb' }}>删除</span>
          ) : (
            <Popconfirm
              title="删除确认"
              description={`确定删除用户「${record.username}」吗？`}
              okText="确定删除"
              cancelText="取消"
              okButtonProps={{ danger: true }}
              onConfirm={() => handleDelete(record.userId)}
            >
              <a style={{ color: 'var(--cinnabar)' }}>删除</a>
            </Popconfirm>
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
            <Form.Item label="用户名">
              <Input
                allowClear
                placeholder="请输入用户名"
                value={query.username}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, username: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item label="真实姓名">
              <Input
                allowClear
                placeholder="请输入真实姓名"
                value={query.realName}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, realName: e.target.value })}
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
        title="用户列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
            新增用户
          </Button>
        }
      >
        <Table
          rowKey="userId"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 900 }}
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
        title={editing ? '编辑用户' : '新增用户'}
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
            <Form.Item label="用户编号">
              <Input value={editing.userId} disabled />
            </Form.Item>
          )}
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="username"
                label="用户名"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="登录用户名" maxLength={50} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="password"
                label={editing ? '重置密码' : '密码'}
                rules={editing ? [] : [{ required: true, message: '请输入密码' }]}
                extra={editing ? '留空表示不修改密码' : undefined}
              >
                <Input.Password placeholder={editing ? '留空不修改' : '请输入密码'} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="realName" label="真实姓名">
                <Input maxLength={50} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="gender" label="性别">
                <Radio.Group>
                  <Radio value="M">男</Radio>
                  <Radio value="F">女</Radio>
                </Radio.Group>
              </Form.Item>
            </Col>
          </Row>
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
