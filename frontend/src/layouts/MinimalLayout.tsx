import {Outlet} from 'react-router-dom';
import {Header} from '../components/widgets/Header/Header.tsx';
import {Footer} from '../components/widgets/Footer/Footer.tsx';

interface MinimalLayoutProps {
    hideHeader?: boolean;
}

export const MinimalLayout = ({hideHeader = false}: MinimalLayoutProps) => {
    return (
        <div style={{display: 'flex', flexDirection: 'column', minHeight: '100vh'}}>
            {!hideHeader && <Header variant="minimal"/>}
            <main style={{flex: 1, marginTop: '70px'}}>
                <Outlet/>
            </main>
            <Footer/>
        </div>
    );
};