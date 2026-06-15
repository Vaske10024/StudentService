export function Loading() {
  return <div className="loadingState" role="status"><span className="spinner" aria-hidden="true" /><span>Loading data...</span></div>;
}

export function ErrorMessage({ message }: { message: string }) {
  return <p className="error" role="alert"><strong>Something went wrong.</strong><span>{message}</span></p>;
}
