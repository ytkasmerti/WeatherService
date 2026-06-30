import { useState } from 'react';
import {FaEye, FaEyeSlash} from "react-icons/fa";

export const PasswordField = ({ placeholder, onChange }: any) => {
    const [show, setShow] = useState(false);
    return (
        <div style={{ display: 'flex', gap: '5px', width: '100%' }}>
            <input
                type={show ? "text" : "password"}
                placeholder={placeholder}
                onChange={(e) => onChange(e.target.value)}
                style={{ width: '100%', padding: '8px' }}
            />
            <button type="button" onClick={() => setShow(!show)}>{show ? <FaEyeSlash /> : <FaEye />}</button>
        </div>
    );
};


