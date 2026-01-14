import { useState, useEffect } from 'react'
import { Layout, Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  UnorderedListOutlined,
  RobotOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'

const { Sider } = Layout

type MenuItem = Required<MenuProps>['items'][number]

const Sidebar = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const [isMobile, setIsMobile] = useState(false)

  // 检测是否为移动端
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768)
      if (window.innerWidth < 768) {
        setCollapsed(true) // 移动端默认折叠
      }
    }
    checkMobile()
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

  const menuItems: MenuItem[] = [
    {
      key: '/',
      icon: <UnorderedListOutlined />,
      label: '任务列表',
    },
    {
      key: '/llm',
      icon: <RobotOutlined />,
      label: '本地LLM',
    },
    // 后续可以添加更多页面
    // {
    //   key: '/settings',
    //   icon: <SettingOutlined />,
    //   label: '设置',
    // },
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
    // 移动端点击后自动折叠
    if (isMobile) {
      setCollapsed(true)
    }
  }

  // 根据当前路径确定选中的菜单项
  const getSelectedKeys = () => {
    const path = location.pathname
    if (path.startsWith('/tasks/')) {
      return ['/'] // 任务详情页也选中任务列表
    }
    return [path]
  }

  return (
    <Sider
      width={200}
      collapsedWidth={isMobile ? 0 : 80}
      collapsed={collapsed}
      collapsible
      trigger={null}
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        zIndex: 1000,
      }}
      theme="dark"
    >
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'center',
          color: '#fff',
          fontSize: collapsed ? 16 : 18,
          fontWeight: 'bold',
          borderBottom: '1px solid #303030',
          padding: collapsed ? '0 16px' : '0 24px',
        }}
      >
        {!collapsed && <FileTextOutlined style={{ marginRight: 8 }} />}
        {!collapsed && <span>Nebula</span>}
        {collapsed && <FileTextOutlined />}
      </div>
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={getSelectedKeys()}
        items={menuItems}
        onClick={handleMenuClick}
        style={{ borderRight: 0 }}
        inlineCollapsed={collapsed}
      />
      <div
        style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          padding: '16px',
          borderTop: '1px solid #303030',
          display: 'flex',
          justifyContent: 'center',
        }}
      >
        <div
          onClick={() => setCollapsed(!collapsed)}
          style={{
            cursor: 'pointer',
            color: '#fff',
            fontSize: 16,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        </div>
      </div>
    </Sider>
  )
}

export default Sidebar

