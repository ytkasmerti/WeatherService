import styles from './Styles.module.css';

interface DeleteModalProps {
    onCancel: () => void;
    onConfirm: () => void;
    isLoading?: boolean;
}

export const DeleteModal = ({onCancel, onConfirm, isLoading}: DeleteModalProps) => {
    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modalContainer}>
                <div className={styles.modalHeader}>
                    <p className={styles.titleText}>Удаление аккаунта</p>
                </div>

                <div className={styles.bodyContent}>
                    <p className={styles.bodyText}>Вы уверены, что хотите удалить аккаунт? Это действие нельзя
                        отменить.</p>
                </div>

                <div className={styles.buttonGroup}>
                    <button
                        className={`${styles.customBtn} ${styles.btnCancel}`}
                        onClick={onCancel}
                        type="button"
                        disabled={isLoading}
                    >
                        <span className={styles.btnTextBlack}>Отмена</span>
                    </button>

                    <button
                        className={`${styles.customBtn} ${styles.btnDelete}`}
                        onClick={onConfirm}
                        type="button"
                        disabled={isLoading}
                    >
                        <span className={styles.btnTextWhite}>
                            {isLoading ? 'Удаление...' : 'Удалить'}
                        </span>
                    </button>
                </div>
            </div>
        </div>
    );
};