import { parseApiError } from '../lib/parseApiError';

/**
 * Helper hook for wiring backend field errors into React Hook Form.
 *
 * Usage:
 *   const { applyServerErrors } = useFormErrors(form)
 *
 *   mutation.mutate(data, {
 *     onError: (error) => {
 *       const { bannerMessage } = applyServerErrors(error)
 *       setBannerError(bannerMessage)
 *     }
 *   })
 *
 * @param {import('react-hook-form').UseFormReturn} form
 */
export function useFormErrors(form) {
  /**
   * Parses the error, sets field-level errors on the RHF form,
   * and returns the top-level message for the banner.
   *
   * @param {unknown} error
   * @returns {{ bannerMessage: string, status: number | null, fieldErrors: Record<string, string> }}
   */
  function applyServerErrors(error) {
    const { message, fieldErrors, status } = parseApiError(error);

    Object.entries(fieldErrors).forEach(([field, msg]) => {
      if (form?.setError) {
        form.setError(field, { type: 'server', message: msg });
      }
    });

    return { bannerMessage: message, status, fieldErrors };
  }

  return { applyServerErrors };
}
