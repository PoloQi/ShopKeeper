import request from '../utils/request'

export const login = (data) => request.post('/api/auth/login', data)
export const logout = () => request.post('/api/auth/logout')
export const info = () => request.get('/api/auth/info')
