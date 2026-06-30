interface ButtonProps {
    label: string;
    onClick: () => void;
    type?: "button" | "submit";
    disabled?: boolean;
}

export const Button = ({ label, onClick, type = "button", disabled = false }: ButtonProps) => {
    return (
        <button className="my-button" type={type} onClick={onClick} disabled={disabled}>
            {label}
        </button>
    );
};