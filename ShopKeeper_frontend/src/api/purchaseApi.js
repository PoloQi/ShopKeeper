import request from '../utils/request'

export const page = (params) => request.get('/api/purchases', { params })
export const getDetail = (poNo) => request.get(`/api/purchases/${poNo}`)
export const save = (data) => request.post('/api/purchases', data)
export const remove = (poNo) => request.delete(`/api/purchases/${poNo}`)
export const audit = (poNo) => request.put(`/api/purchases/${poNo}/audit`)
