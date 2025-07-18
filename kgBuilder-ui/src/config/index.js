// base url
const BASE_URL = process.env.VUE_APP_BASE_URL;

// 从 localStorage 中获取初始值
let _isTeacher = localStorage.getItem('isTeacher') === 'true';

// 设置方法
export const setIsTeacher = (value) => {
    _isTeacher = value;
    // 将值存储到 localStorage 中
    localStorage.setItem('isTeacher', value);
};

// 获取方法
export const getIsTeacher = () => {
    // 从 localStorage 中更新值
    _isTeacher = localStorage.getItem('isTeacher') === 'true';
    return _isTeacher;
};

export { BASE_URL };
