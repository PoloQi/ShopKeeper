import { useEffect, useState } from 'react'
import {
  Table,
  Button,
  Form,
  Input,
  Select,
  DatePicker,
  Space,
  Card,
  Row,
  Col,
  message,
  Popconfirm,
  Tag
} from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import * as saleApi from '../api/saleApi'
import * as customerApi from '../api/customerApi'

const emptyQuery = {
  soNo: '',
  customerId: undefined,
  status: undefined,
  dateRange: null
}

const toParams = (q) => ({
  soNo: q.soNo || undefined,
  customerId: q.customerId,
  status: q.status,
  startDate: q.dateRange ? q.dateRange[0].format('YYYY-MM-DD') : undefined,
  endDate: q.dateRange ? q.dateRange[1].format('YYYY-MM-DD') : undefined
})

export default function Sale() {
  const navigate = useNavigate()
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [customers, setCustomers] = useState([])

  const [query, setQuery] = useState(emptyQuery)
  const [applied, setApplied] = useState(emptyQuery)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await saleApi.page({ ...toParams(applied), page, size })
      setData(res.data.records)
      setTotal(res.data.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    customerApi.options().then((res) => setCustomers(res.data))
  }, [])

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

  const handleAudit = async (soNo) => {
    await saleApi.audit(soNo)
    message.success('审核成功')
    loadData()
  }

  const handleDelete = async (soNo) => {
    await saleApi.remove(soNo)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '销售单号', dataIndex: 'soNo', width: 160 },
    { title: '客户', dataIndex: 'customerName' },
    { title: '业务日期', dataIndex: 'orderDate', width: 120 },
    { title: '交货地点', dataIndex: 'deliveryPlace', width: 120 },
    {
      title: '合计金额(元)',
      dataIndex: 'totalAmount',
      width: 130,
      align: 'right'
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (s) =>
        s === '1' ? <Tag color="green">已审核</Tag> : <Tag color="orange">未审核</Tag>
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      render: (_, record) => {
        const audited = record.status === '1'
        return (
          <Space size="middle">
            {audited ? (
              <span style={{ color: '#bbb' }} title="已审核单据不能修改">
                编辑
              </span>
            ) : (
              <a onClick={() => navigate(`/sale/edit/${record.soNo}`)}>编辑</a>
            )}
            {audited ? (
              <span style={{ color: '#bbb' }}>审核</span>
            ) : (
              <Popconfirm
                title="审核确认"
                description={`确定审核销售单「${record.soNo}」吗？审核后将扣减库存。`}
                okText="确定审核"
                cancelText="取消"
                onConfirm={() => handleAudit(record.soNo)}
              >
                <a>审核</a>
              </Popconfirm>
            )}
            {audited ? (
              <span style={{ color: '#bbb' }} title="已审核单据不能删除">
                删除
              </span>
            ) : (
              <Popconfirm
                title="删除确认"
                description={`确定删除销售单「${record.soNo}」吗？`}
                okText="确定删除"
                cancelText="取消"
                okButtonProps={{ danger: true }}
                onConfirm={() => handleDelete(record.soNo)}
              >
                <a style={{ color: 'var(--cinnabar)' }}>删除</a>
              </Popconfirm>
            )}
          </Space>
        )
      }
    }
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }} styles={{ body: { paddingBottom: 0 } }}>
        <Row gutter={16}>
          <Col span={5}>
            <Form.Item label="销售单号">
              <Input
                allowClear
                placeholder="请输入销售单号"
                value={query.soNo}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, soNo: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item label="客户">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder="全部客户"
                value={query.customerId}
                onChange={(v) => setQuery({ ...query, customerId: v })}
                options={customers.map((c) => ({
                  value: c.customerId,
                  label: `${c.customerId} ${c.customerName}`
                }))}
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
                  { value: '0', label: '未审核' },
                  { value: '1', label: '已审核' }
                ]}
              />
            </Form.Item>
          </Col>
          <Col span={7}>
            <Form.Item label="业务日期">
              <DatePicker.RangePicker
                value={query.dateRange}
                onChange={(v) => setQuery({ ...query, dateRange: v })}
              />
            </Form.Item>
          </Col>
          <Col span={3}>
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
        title="销售单列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/sale/new')}>
            新增销售单
          </Button>
        }
      >
        <Table
          rowKey="soNo"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1100 }}
          pagination={{
            current: page,
            pageSize: size,
            total,
            showSizeChanger: true,
            showTotal:t => `共 ${t} 条`,
            onChange: (p, s) => {
              setPage(p)
              setSize(s)
            }
          }}
        />
      </Card>
    </div>
  )
}
