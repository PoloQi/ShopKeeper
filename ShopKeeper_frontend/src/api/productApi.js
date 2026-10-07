import request from '../utils/request'

export const page = (params) => request.get('/api/products', { params })
export const options = () => request.get('/api/products/options')
export const getById = (id) => request.get(`/api/products/${id}`)
export const add = (data) => request.post('/api/products', data)
export const update = (data) => request.put('/api/products', data)
export const remove = (id) => request.delete(`/api/products/${id}`)
