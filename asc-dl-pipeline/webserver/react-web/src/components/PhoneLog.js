import React, { useEffect, useRef } from 'react';
import Phone from '../assets/empty_phone.png';
import { useImmer } from 'use-immer';
import useWindowDimensions from '../hooks/useWindowDimensions';


const PhoneLog = ({items}) => {
    const dimensions = useWindowDimensions(); //only for resizing font
    const heightRef = useRef(null);
    const [height, setHeight] = useImmer(0);
    const lastItemRef = useRef(null);

    useEffect(() => {
        if (heightRef.current) {
            setHeight(heightRef.current.clientHeight)
        }
        if (lastItemRef) {
            lastItemRef.current.scrollIntoView();
        }        
    }, [setHeight, dimensions, items])

    return <div style={{flex: 1, display: 'flex', justifyContent: 'center', alignContent: 'center', position: 'relative'}}>
        <img style={{width: '90%', maxHeight: '65vh', display: 'flex'}} src={Phone} alt="Phone with logging" />
        <div style={{
            height: '79%',
            width: '83%',
            maxWidth: '50vw',
            backgroundColor: 'black',
            alignItems: 'flex-start',
            top: 0,
            position: 'absolute',
            marginTop: '18%',
            // fontSize: '75%',
            fontSize: height ? `${height/25}px` : null,
            textAlign: 'start',
            overflowY: 'auto',
        }} ref={heightRef}>
        {items.map((e, i) => (
            <div
                ref={ref => {
                    if (i === items.length - 1) {
                        lastItemRef.current = ref;
                    }
                }}
                key={e + Math.random()}
                style={{
                    height: '10%',
                    marginLeft: '5%',
                }}
            >
                <code>{e}</code>
            </div>
        ))}
        </div>
    </div>
}
export default PhoneLog;