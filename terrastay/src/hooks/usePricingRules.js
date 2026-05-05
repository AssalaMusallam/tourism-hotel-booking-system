import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  pricingRuleKeys,
  getAllPricingRules,
  getActivePricingRules,
  getPricingRule,
  createPricingRule,
  updatePricingRule,
  deletePricingRule,
  getPricePreview,
  getRoomPricePreview,
} from '../api/pricingRulesApi';

export const usePricingRules = (page = 0, activeOnly = false) =>
  useQuery({
    queryKey: pricingRuleKeys.list(page, activeOnly),
    queryFn: () =>
      activeOnly
        ? getActivePricingRules({ page, size: 10 })
        : getAllPricingRules({ page, size: 10 }),
    staleTime: 2 * 60 * 1000,
  });

export const usePricingRule = (id) =>
  useQuery({
    queryKey: pricingRuleKeys.detail(id),
    queryFn: () => getPricingRule(id),
    enabled: !!id,
  });

export const useCreatePricingRule = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createPricingRule,
    onSuccess: () => qc.invalidateQueries({ queryKey: pricingRuleKeys.all }),
  });
};

export const useUpdatePricingRule = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: updatePricingRule,
    onSuccess: () => qc.invalidateQueries({ queryKey: pricingRuleKeys.all }),
  });
};

export const useDeletePricingRule = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deletePricingRule,
    onSuccess: () => qc.invalidateQueries({ queryKey: pricingRuleKeys.all }),
  });
};

export const usePricePreview = (basePrice, checkIn, checkOut, enabled = true) =>
  useQuery({
    queryKey: pricingRuleKeys.preview(basePrice, checkIn, checkOut),
    queryFn: () => getPricePreview({ basePrice, checkIn, checkOut }),
    enabled: !!(basePrice && checkIn && checkOut && enabled),
    staleTime: 5 * 60 * 1000,
  });

export const useRoomPricePreview = (roomTypeId, checkIn, checkOut) =>
  useQuery({
    queryKey: ['roomPreview', roomTypeId, checkIn, checkOut],
    queryFn: () => getRoomPricePreview(roomTypeId, { checkIn, checkOut }),
    enabled: !!(roomTypeId && checkIn && checkOut),
    staleTime: 5 * 60 * 1000,
  });
