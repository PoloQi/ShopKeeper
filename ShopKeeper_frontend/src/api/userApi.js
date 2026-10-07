import request from '../utils/request'

export const page = (params) => request.get('/api/users', { params })
export const add = (data) => request.post('/api/users', data)
export const update = (data) => request.put('/api/users', data)
export const remove = (id) => request.delete(`/api/users/${id}`)
