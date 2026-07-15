import * as yup from 'yup';

export const weatherSchema = yup.object({
    date: yup.string().when('$type', {
        is: (type: string) => type === 'hourly' || type === 'history',
        then: (schema) => schema.test(
            'date-or-range',
            'Выберите дату или период дат для прогноза',
            function (value) {
                const {start, end} = this.parent;

                return Boolean(value) || Boolean(start && end);
            }
        )
    }),
    days: yup.string().when('$type', {
        is: (type: string) => type === 'forecast' || type === 'filter',
        then: (schema) => schema
            .required('Укажите количество дней')
            .test('is-number', 'Количество дней должно быть числом', val => !isNaN(parseInt(val!)))
            .test('range', 'Количество дней должно быть от 1 до 14', val => {
                const num = parseInt(val!);
                return num >= 1 && num <= 14;
            }),
        otherwise: (schema) => schema.optional()
    }),
    city: yup.string().when('$type', {
        is: (type: string) => type !== 'coords',
        then: (schema) => schema
            .required('Название города обязательно')
            .matches(/^[a-zA-Zа-яёА-ЯЁ\s-]+$/, 'Название города может содержать только буквы'),
        otherwise: (schema) => schema.optional()
    }),
    lon: yup.string().when('$type', {
        is: 'coords',
        then: (schema) => schema
            .required('Укажите долготу')
            .test('is-number', 'Долгота должна быть числом', val => !isNaN(parseFloat(val!)))
            .test('range', 'Значения долготы должны быть от -180 до 180', val => {
                const num = parseFloat(val!);
                return num >= -180 && num <= 180;
            }),
        otherwise: (schema) => schema.optional()
    }),
    lat: yup.string().when('$type', {
        is: 'coords',
        then: (schema) => schema
            .required('Укажите широту')
            .test('is-number', 'Широта должна быть числом', val => !isNaN(parseFloat(val!)))
            .test('range', 'Значения широты должны быть от -90 до 90', val => {
                const num = parseFloat(val!);
                return num >= -90 && num <= 90;
            }),
        otherwise: (schema) => schema.optional()
    }),
});

