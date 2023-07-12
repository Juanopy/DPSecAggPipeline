import React from 'react';
import Stylesheet from 'reactjs-stylesheet';
import logo from '../assets/uni_de.png';
import useWindowDimensions from '../hooks/useWindowDimensions';
import LeftView from './LeftView';
import RightView from './RightView';

const MainView = () => {
    let [width, height] = useWindowDimensions();

    return <div style={styles.mainviewBase}>
        <div style={styles.headerDiv}>
            <img style={styles.appLogo} src={logo} alt="logo" />
            <p style={{
                color: 'black',
                alignSelf: 'center',
                paddingRight: '2vw',
            }}>funded by VW Stiftung</p>
        </div>
        <div style={{flex: 1, display: 'flex', flexDirection: width > height ? "row" : 'column'}}>
            <LeftView />
            <RightView />
        </div>
    </div>
}

const styles = Stylesheet.create({
    mainviewBase: {
        width: '100vw',
        height: '100vh'
    },
    headerDiv: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    appLogo: {
        alignSelf: 'flex-start',
        display: 'flex',
        maxHeight: "20vh",
        maxWidth: '100vw',
        pointerEvents: "none",
    }
});

export default MainView