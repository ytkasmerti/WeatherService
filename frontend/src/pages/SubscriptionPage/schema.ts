import * as yup from 'yup';

export const subscriptionSchema = yup.object({
    city: yup.string()
        .trim()
        .required('Название города обязательно')
        .min(2, 'Слишком короткое название')
        .max(50, 'Слишком длинное название')
        .matches(/^[a-zA-Zа-яёА-ЯЁ\s-]+$/, 'Название города может содержать только буквы')
});