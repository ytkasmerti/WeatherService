interface InputProps {
    placeholder: string;
    value: string;
    onChange: (val: string) => void;
    type?: string;
}

export const Input = ({ placeholder, value, onChange, type = "text" }: InputProps) => (
    <input
        style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
    />
);