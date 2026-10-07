import { Form, Input, Button, Spin, message } from 'antd'
import {
  LockOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  TransactionOutlined,
  InboxOutlined
} from '@ant-design/icons'
import { useNavigate, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { user, loading, login } = useAuth()
  const navigate = useNavigate()

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
        <Spin size="large" />
      </div>
    )
  }
  if (user) return <Navigate to="/" replace />

  const onFinish = async (values) => {
    await login(values)
    message.success('登录成功')
    navigate('/', { replace: true })
  }

  return (
    <div className="sk-login">
      <div className="grain-overlay" />

      {/* 品牌区 */}
      <div className="sk-login-brand">
        <div className="sk-login-top">
          <div className="sk-seal">店</div>
          <div>
            <div className="sk-brand-name">店管家</div>
            <div className="sk-brand-sub">SHOPKEEPER</div>
          </div>
        </div>

        <div className="sk-login-headline">
          <h1>一店之账<br />尽在掌握</h1>
          <p>采购 · 销售 · 库存，一体化进销存管理</p>
        </div>

        <div className="sk-login-feats">
          <div className="sk-login-feat">
            <span className="ic">
              <ShoppingCartOutlined />
            </span>
            采购入库，单据可溯
          </div>
          <div className="sk-login-feat">
            <span className="ic">
              <TransactionOutlined />
            </span>
            销售出库，一笔一清
          </div>
          <div className="sk-login-feat">
            <span className="ic">
              <InboxOutlined />
            </span>
            库存结余，一目了然
          </div>
        </div>

        <div className="sk-login-foot">信管专业综合设计 · 课程项目</div>
      </div>

      {/* 表单区 */}
      <div className="sk-login-formwrap">
        <div className="sk-login-card">
          <div className="sk-login-seal-mobile">店</div>
          <h2>欢迎登录</h2>
          <div className="sub">请输入您的账号信息</div>
          <Form onFinish={onFinish} size="large" initialValues={{ username: 'admin' }}>
            <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined />} placeholder="用户名" autoComplete="username" />
            </Form.Item>
            <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="密码"
                autoComplete="current-password"
              />
            </Form.Item>
            <Form.Item style={{ marginBottom: 0 }}>
              <Button type="primary" htmlType="submit" block>
                登 录
              </Button>
            </Form.Item>
          </Form>
          <div className="sk-login-demo">
            演示账号 <b>admin</b> / <b>123456</b>
          </div>
        </div>
      </div>
    </div>
  )
}
