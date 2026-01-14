import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Layout } from 'antd'
import TaskList from './pages/TaskList'
import TaskDetail from './pages/TaskDetail'
import LocalLLM from './pages/LocalLLM'
import Header from './components/Header'
import Sidebar from './components/Sidebar'
import './App.css'

const { Content } = Layout

function App() {
  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        <Sidebar />
        <Layout
          style={{
            marginLeft: 200, // 侧边栏宽度，使用 CSS 媒体查询在移动端覆盖
            transition: 'margin-left 0.2s',
          }}
          className="main-layout"
        >
          <Header />
          <Content
            style={{
              padding: '24px',
              background: '#f0f2f5',
              minHeight: 'calc(100vh - 64px)',
            }}
          >
            <Routes>
              <Route path="/" element={<TaskList />} />
              <Route path="/tasks/:taskId" element={<TaskDetail />} />
              <Route path="/llm" element={<LocalLLM />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </BrowserRouter>
  )
}

export default App

