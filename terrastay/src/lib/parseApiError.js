/**
 * Normalize backend ApiError, AxiosError, Error, and unknown thrown values.
 *
 * @param {unknown} error
 * @returns {{ message: string, fieldErrors: Record<string, string>, status: number | null }}
 */
export function parseApiError(error) {
  const genericMessage = 'Something went wrong. Please try again later.';
  const networkMessage = 'Connection error. Check your internet connection.';

  try {
    const response = error?.response;

    if (!response) {
      const isAxiosNetworkError = error?.isAxiosError || error?.request;
      const message = typeof error?.message === 'string' ? error.message.trim() : '';

      return {
        message: isAxiosNetworkError ? networkMessage : message || genericMessage,
        fieldErrors: {},
        status: null,
      };
    }

    const data = response.data && typeof response.data === 'object' ? response.data : {};
    const status = Number(response.status || data.status) || null;
    const backendMessage = typeof data.message === 'string' ? data.message.trim() : '';
    const fieldErrors = {};

    if (Array.isArray(data.errors)) {
      data.errors.forEach((item) => {
        const field = typeof item?.field === 'string' ? item.field.trim() : '';
        const message = typeof item?.message === 'string' ? item.message.trim() : '';

        if (field && message && !fieldErrors[field]) {
          fieldErrors[field] = message;
        }
      });
    }

    const hasFieldErrors = Object.keys(fieldErrors).length > 0;
    let message = backendMessage;

    if (status === 400 && hasFieldErrors) {
      message = backendMessage || 'Please fix the highlighted fields.';
    } else if (status === 401) {
      message = 'Invalid email or password';
    } else if (status === 403) {
      message =
        backendMessage && /\b(disabled|inactive|deactivated|locked|suspended)\b/i.test(backendMessage)
          ? backendMessage
          : "You don't have permission to perform this action";
    } else if (status === 500 || (status && status >= 500)) {
      message = genericMessage;
    } else if (!message) {
      message = genericMessage;
    }

    return { message, fieldErrors, status };
  } catch {
    return { message: genericMessage, fieldErrors: {}, status: null };
  }
}
