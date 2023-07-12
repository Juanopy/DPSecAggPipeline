import { deepEqual } from 'fast-equals';
import { useCallback, useEffect, useRef } from 'react';
import { useImmer } from 'use-immer';
import { minObject } from '../utils/dataMapping';

export default function useData() {
    const intervallRef = useRef(null);
    const [data, setData] = useImmer({
        logs: [],
        charts: {},
        hist: {}
    });
    const updateData = useCallback((dat) => {
        setData(draft => {
            if (!deepEqual(draft.logs, dat.logs)) {
                draft.logs = dat.logs;
                draft.charts = {...minObject, ...dat.charts};
            }
        })
    }, [setData]);

    const updateHist = useCallback((hist) => {
        setData(draft => {
            draft.hist = hist;
        })
    }, [setData]);

    useEffect(() => {
        function fetcher(path, setter) {
            path = (process.env.NODE_ENV !== 'production' ? '' : '/privee-website') + path;
            fetch(path)
                .then(result => result.json())
                .then(setter);
        }
        fetcher('/data', updateData);
        fetcher('/hist', updateHist);
        intervallRef.current = setInterval(() => fetcher('/data', updateData), 5000);
        return () => {
            clearInterval(intervallRef.current);
        }
    }, [updateData, updateHist]);

    return data;
}