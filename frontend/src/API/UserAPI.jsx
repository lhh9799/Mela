import axios from 'axios'

let ACCESS_TOKEN = localStorage.getItem('accessToken')

export const UserApi = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${ACCESS_TOKEN}`,
    },
})


// 토큰 유효성 검사
UserApi.interceptors.response.use((response) => {
    return response
}, async (error) => {
    const originalRequest = error.config
    if (error.response.status === 403 && !originalRequest._retry) {
        return UserApi(originalRequest)
    }
    return Promise.reject(error)
})

// 회원조회
export const fetchUser = async () => {
    const response = await UserApi.get(`/api/v1/users/myinfo`)
    console.log(response.data)
    return response.data
}

// 회원수정
export const updateUser = async (formdata) => {
    try {
        const response = await axios.put('http://localhost:8080/api/v1/users/myinfo', formdata, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization' : `Bearer ${localStorage.accessToken}`
          }
        })
        return response.data
      }
      catch (error) {
        console.error(error);
      }
}

// 회원탈퇴
export const deleteUser = async () => {
    await UserApi.delete(`api/v1/users/delete`)
    localStorage.clear()
}

// 나를 팔로우 한 사람 조회
export const follower = async (userId) => {
    const response = await UserApi.get(`/api/v1/users/${userId}/followees`)
    console.log(response.data)
    return response.data
}

// 내가 팔로우 한 사람 조회
export const followee = async (userId) => {
    const response = await UserApi.get(`/api/v1/users/${userId}/followers`)
    console.log(response.data)
    return response.data
}

// 팔로우 요청 
export const followUser = async (userId) => {
    const response = await UserApi.put(`/api/v1/users/follow/${userId}`)
    console.log(response.data)
    return response.data
}

// 이메일 중복 확인
export const emailCheck = async ({emailId}) => {
    console.log(emailId)
    const response = await axios.get(`http://localhost:8080/api/v1/users/emailid/${emailId}`)
    console.log(response)
    
    return response.data
}

// 새로운 비밀번호 설정(잃어버렸을 때)
export const newPassword = async (data) => {
    const response = await UserApi.put(`api/v1/users/users/newpassword`, data)
    console.log(response)
    return response.data
}