import { Layout, Menu, Spin, Button, Tooltip } from 'antd'
import { useNavigate, useLocation, Navigate, Outlet } from 'react-router-dom'
import {
  TeamOutlined,
  ShopOutlined,
  ShoppingOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  InboxOutlined,
  AppstoreOutlined,
  TransactionOutlined,
  LogoutOutlined
} from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'

const { Header, Sider, Content } = Layout

const menuItems = [
  {
    key: 'base',
    icon: <AppstoreOutlined />,
    label: '基础档案',
    children: [
      { key: '/customer', icon: <TeamOutlined />, label: '客户管理' },
      { key: '/supplier', icon: <ShopOutlined />, label: '供应商管理' },
      { key: '/product', icon: <ShoppingOutlined />, label: '商品管理' },
      { key: '/user', icon: <UserOutlined />, label: '用户管理' }
    ]
  },
  {
    key: 'purchase-group',
    icon: <ShoppingCartOutlined />,
    label: '采购管理',
    children: [{ key: '/purchase', label: '采购单管理' }]
  },
  {
    key: 'sale-group',
    icon: <TransactionOutlined />,
    label: '销售管理',
    children: [{ key: '/sale', label: '销售单管理' }]
  },
  { key: '/stock', icon: <InboxOutlined />, label: '库存查询' }
]

const titleMap = {
  '/customer': '客户管理',
  '/supplier': '供应商管理',
  '/product': '商品管理',
  '/user': '用户管理',
  '/purchase': '采购单管理',
  '/sale': '销售单管理',
  '/stock': '库存查询'
}

export default function MainLayout() {
  const { user, loading, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
        <Spin size="large" />
      </div>
    )
  }
  if (!user) return <Navigate to="/login" replace />

  // 列表页与编辑页都高亮对应菜单
  const matchKey =
    ['/customer', '/supplier', '/product', '/user', '/purchase', '/sale', '/stock'].find(
      (k) => location.pathname.startsWith(k)
    ) || location.pathname

  const currentTitle =
    Object.entries(titleMap).find(([k]) => location.pathname.startsWith(k))?.[1] || '店管家'

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <Layout style={{ height: '100%' }}>
      <div className="grain-overlay" />
      <Sider className="sk-sider" width={232}>
        <div className="sk-brand">
          <div className="sk-seal">店</div>
          <div>
            <div className="sk-brand-name">店管家</div>
            <div className="sk-brand-sub">SHOPKEEPER</div>
          </div>
        </div>
        <div className="sk-sider-divider" />
        <Menu
          className="sk-menu"
          theme="dark"
          mode="inline"
          selectedKeys={[matchKey]}
          defaultOpenKeys={['base', 'purchase-group', 'sale-group']}
          items={menuItems}
          onClick={({ key }) => {
            if (key.startsWith('/')) navigate(key)
          }}
        />
      </Sider>
      <Layout className="sk-inner-layout">
        <Header className="sk-header">
          <div className="sk-header-title">
            <span className="dot" />
            {currentTitle}
          </div>
          <div className="sk-userbox">
            <div className="sk-userchip">
              <div className="sk-avatar">{(user.realName || user.username || '?').charAt(0)}</div>
              <div>
                <div className="sk-user-name">{user.realName || user.username}</div>
                <div className="sk-user-role">系统用户</div>
              </div>
            </div>
            <Tooltip title="退出登录">
              <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout} />
            </Tooltip>
          </div>
        </Header>
        <Content className="sk-content">
          <div key={location.pathname} className="page-enter">
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}
