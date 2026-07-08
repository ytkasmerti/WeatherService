import * as yup from 'yup';

export const registerSchema = yup.object({
    email: yup.string()
        .email('Некорректный email')
        .required('Введите свой email'),

    password: yup.string()
        .min(6, 'Пароль не может быть меньше 6 символов')
        .max(50, 'Пароль не может быть больше 50 символов')
        .required('Пароль обязателен'),

    confirm: yup.string()
        .oneOf([yup.ref('password')], 'Пароли не совпадают')
        .required('Подтвердите пароль')
});