// 스토어를 만들어야 합니다. 괴도 ㅇㅂㅇ

import axios from "axios";
import { create } from "zustand";

const useStore = create(set => ({
  islogined: localStorage.accessToken ? true : false,
  setIsLogined : (logined) => set({ islogined: logined}),
  user : null,
  fetchUser: async () => {
    if (!localStorage.accessToken) {
      console.log('로그인되지 않음')
      return}
    try {
      const response = await axios.get(`http://localhost:8080/api/v1/users/myinfo`,{
        headers : {
          'Authorization' :`Bearer ${localStorage.accessToken}`
        }
      });
      set({user: response.data})
    }
     catch(error) {
      console.error(error)
    }
  },

  logout : async () => {
    try {
      await axios.get('http://localhost:8080/api/v1/auth/logout', {
        headers : {
          'Authorization' :`Bearer ${localStorage.accessToken}`
        }
      });
      set({user: null});
      localStorage.clear();
      set({islogined: false});
      alert('로그아웃');
      window.location.href = `/`
    } catch (error) {
      console.error('Logout failed', error);
    }
  },
}))


export default useStore