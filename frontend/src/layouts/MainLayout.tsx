import {Outlet} from 'react-router-dom';
import {Header} from '../components/ui/Header/Header.tsx';
import {Footer} from '../components/ui/Footer/Footer.tsx';

export const MainLayout = () => {
    return (
        <div style={{display: 'flex', flexDirection: 'column', minHeight: '100vh'}}>
            <Header variant="default"/>
            <main style={{flex: 1, marginTop: '70px'}}>
                <Outlet/>
            </main>
            <Footer/>
        </div>
    );
};