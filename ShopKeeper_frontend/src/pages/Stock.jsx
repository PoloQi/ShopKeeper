import { useEffect, useState } from 'react'
import {
  Table,
  Button,
  Form,
  Input,
  Checkbox,
  Space,
  Card,
  Row,
  Col
} from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import * as stockApi from '../api/stockApi'

const emptyQuery = { productName: '', productId: '', onlyPositive: false }

export default function Stock() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)

  const [query, setQuery] = useState(emptyQuery)
  const [applied, setApplied] = useState(emptyQuery)

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await stockApi.list({
        productName: applied.productName || undefined,
        productId: applied.productId || undefined,
        onlyPositive: applied.onlyPositive
      })
      setData(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [applied])

  const handleSearch = () => setApplied({ ...query })

  const handleReset = () => {
    setQuery(emptyQuery)
    setApplied(emptyQuery)
  }

  const columns = [
    { title: '商品编号', dataIndex: 'productId', width: 110 },
    { title: '商品名称', dataIndex: 'productName' },
    { title: '规格型号', dataIndex: 'spec', width: 140 },
    { title: '单位', dataIndex: 'unit', width: 70 },
    { title: '单价(元)', dataIndex: 'unitPrice', width: 100, align: 'right' },
    { title: '累计入库', dataIndex: 'purchaseQty', width: 100, align: 'right' },
    { title: '累计出库', dataIndex: 'saleQty', width: 100, align: 'right' },
    {
      title: '当前库存',
      dataIndex: 'stockQty',
      width: 110,
      align: 'right',
      render: (q) => (
        <span style={{ color: q > 0 ? 'var(--pine)' : 'var(--cinnabar)', fontWeight: 600 }}>{q}</span>
      )
    }
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }} styles={{ body: { paddingBottom: 0 } }}>
        <Row gutter={16}>
          <Col span={5}>
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
            <Form.Item label="商品编号">
              <Input
                allowClear
                placeholder="请输入商品编号"
                value={query.productId}
                onPressEnter={handleSearch}
                onChange={(e) => setQuery({ ...query, productId: e.target.value })}
              />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item label=" ">
              <Checkbox
                checked={query.onlyPositive}
                onChange={(e) =>
                  setQuery({ ...query, onlyPositive: e.target.checked })
                }
              >
                仅看有库存
              </Checkbox>
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

      <Card title="实时库存（数据来自数据库视图 v_stock）">
        <Table
          rowKey="productId"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 900 }}
          pagination={false}
        />
      </Card>
    </div>
  )
}
