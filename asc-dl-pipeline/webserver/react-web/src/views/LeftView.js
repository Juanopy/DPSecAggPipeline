import React from 'react';
import PhoneLog from '../components/PhoneLog';
import useData from '../hooks/useData';

const LeftView = () => {
    const logs = useData().logs;

    return <div style={{flex: 2, display: 'flex', justifyContent : 'space-around', flexDirection: "row"}}>
        {
            Object.keys(logs).map((k, i) => (
                <div key={k} style={{flexDirection: 'column'}}>
                    <p style={{alignSelf: 'center'}}>Device {i+1}</p>
                    <code style={{alignSelf: 'center'}}>({k})</code>
                    <PhoneLog items={logs[k]} />
                </div>
            ))
        }
    </div>
}

export default LeftView