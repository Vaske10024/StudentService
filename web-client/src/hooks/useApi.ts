import { useCallback, useEffect, useState } from 'react';
import type React from 'react';
import { apiErrorMessage } from '../api/client';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  reload: () => Promise<void>;
  setData: React.Dispatch<React.SetStateAction<T | null>>;
}

export function useApi<T>(loader: () => Promise<T>, deps: React.DependencyList = []): UseApiState<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await loader());
    } catch (err) {
      setError(apiErrorMessage(err, 'Unexpected error.'));
    } finally {
      setLoading(false);
    }
  }, deps);

  useEffect(() => { void reload(); }, [reload]);

  return { data, loading, error, reload, setData };
}
