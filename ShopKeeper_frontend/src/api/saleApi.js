import request from '../utils/request'

export const page = (params) => request.get('/api/sales', { params })
export const getDetail = (soNo) => request.get(`/api/sales/${soNo}`)
export const save = (data) => request.post('/api/sales', data)
export const remove = (soNo) => request.delete(`/api/sales/${soNo}`)
export const audit = (soNo) => request.put(`/api/sales/${soNo}/audit`)
