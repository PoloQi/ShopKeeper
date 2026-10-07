import request from '../utils/request'

export const list = (params) => request.get('/api/stock', { params })
