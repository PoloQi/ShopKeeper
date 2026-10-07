import request from '../utils/request'

export const page = (params) => request.get('/api/suppliers', { params })
export const options = () => request.get('/api/suppliers/options')
export const getById = (id) => request.get(`/api/suppliers/${id}`)
export const add = (data) => request.post('/api/suppliers', data)
export const update = (data) => request.put('/api/suppliers', data)
export const remove = (id) => request.delete(`/api/suppliers/${id}`)
