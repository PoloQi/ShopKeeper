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
import * as purchaseApi from '../api/purchaseApi'
import * as supplierApi from '../api/supplierApi'
import { useAuth } from '../context/AuthContext'

const emptyQuery = {
  poNo: '',
  supplierId: undefined,
  status: undefined,
  dateRange: null
}

const toParams = (q) => ({
  poNo: q.poNo || undefined,
  supplierId: q.supplierId,
  status: q.status,
  startDate: q.dateRange ? q.dateRange[0].format('YYYY-MM-DD') : undefined,
  endDate: q.dateRange ? q.dateRange[1].format('YYYY-MM-DD') : undefined
})

export default function Purchase() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [suppliers, setSuppliers] = useState([])

  const [query, setQuery] = useState(emptyQuery)
  const [applied, setApplied] = useState(emptyQuery)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await purchaseApi.page({ ...toParams(applied), page, size })
      setData(res.data.records)
      setTotal(res.data.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    supplierApi.options().then((res) => setSuppliers(res.data))
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

  const handleDelete = async (poNo) => {
    await purchaseApi.remove(poNo)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '采购单号', dataIndex: 'poNo', width: 160 },
    { title: '供应商', dataIndex: 'supplierName' },
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
              <a onClick={() => navigate(`/purchase/view/${record.poNo}`)}>查看</a>
            ) : (
              <a onClick={() => navigate(`/purchase/edit/${record.poNo}`)}>编辑</a>
            )}
            {audited || user.role !== 1 ? (
              <span style={{ color: '#bbb' }}>审核</span>
            ) : (
              <a onClick={() => navigate(`/purchase/audit/${record.poNo}`)}>审核</a>
            )}
            {audited || user.role !== 1 ? (
              <span
                style={{ color: '#bbb' }}
                title={audited ? '已审核单据不能删除' : '需要店长权限'}
              >
                删除
              </span>
            ) : (
              <Popconfirm
                title="删除确认"
                description={`确定删除采购单「${record.poNo}」吗？`}
                okText="确定删除"
                cancelText="取消"
                okButtonProps={{ danger: true }}
                onConfirm={() => handleDelete(record.poNo)}
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
            <Form.Item label="采购单号">
              <Input
                allowClear
                placeholder="请输入采购单号"
                value={query.poNo}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, poNo: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={5}>
            <Form.Item label="供应商">
              <Select
                allowClear
                showSearch
                optionFilterProp="label"
                placeholder="全部供应商"
                value={query.supplierId}
                onChange={(v) => setQuery({ ...query, supplierId: v })}
                options={suppliers.map((s) => ({
                  value: s.supplierId,
                  label: `${s.supplierId} ${s.supplierName}`
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
        title="采购单列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/purchase/new')}>
            新增采购单
          </Button>
        }
      >
        <Table
          rowKey="poNo"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1100 }}
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
    </div>
  )
}
