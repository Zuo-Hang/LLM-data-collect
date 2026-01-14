import { Layout, Typography } from 'antd'

const { Header: AntHeader } = Layout
const { Title } = Typography

const Header = () => {
  return (
    <AntHeader
      style={{
        background: '#fff',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        position: 'sticky',
        top: 0,
        zIndex: 100,
      }}
    >
      <Title level={4} style={{ margin: 0, color: '#001529' }}>
        AI Agent Orchestrator
      </Title>
    </AntHeader>
  )
}

export default Header

