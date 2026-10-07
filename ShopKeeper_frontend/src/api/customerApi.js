import request from '../utils/request'

export const page = (params) => request.get('/api/customers', { params })
export const options = () => request.get('/api/customers/options')
export const getById = (id) => request.get(`/api/customers/${id}`)
export const add = (data) => request.post('/api/customers', data)
export const update = (data) => request.put('/api/customers', data)
export const remove = (id) => request.delete(`/api/customers/${id}`)
