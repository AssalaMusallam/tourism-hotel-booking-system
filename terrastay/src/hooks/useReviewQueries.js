import { useMutation, useQueryClient } from '@tanstack/react-query';
import { reviewKeys, submitReview } from '../api/reviews';

export const useSubmitReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitReview,
    onSuccess: (review) => {
      queryClient.invalidateQueries({ queryKey: reviewKeys.all });
      queryClient.invalidateQueries({ queryKey: reviewKeys.hotel(review.hotelId) });
    },
  });
};
