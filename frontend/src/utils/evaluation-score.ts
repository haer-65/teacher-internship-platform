export interface WeightedScoreItemLike {
  itemScore?: number | null;
  itemWeight?: number | null;
}

export function calculateWeightedScore(
  items: WeightedScoreItemLike[]
): number | undefined {
  if (!items.length) {
    return undefined;
  }

  let weightedTotal = 0;
  let weightTotal = 0;

  for (const item of items) {
    if (item.itemScore === undefined || item.itemScore === null) {
      return undefined;
    }
    if (item.itemWeight === undefined || item.itemWeight === null) {
      return undefined;
    }
    weightedTotal += Number(item.itemScore) * Number(item.itemWeight);
    weightTotal += Number(item.itemWeight);
  }

  if (Math.abs(weightTotal - 100) > 0.01) {
    return undefined;
  }

  return Number((weightedTotal / 100).toFixed(2));
}
