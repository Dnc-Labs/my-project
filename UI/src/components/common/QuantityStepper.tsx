import { MinusIcon, PlusIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface QuantityStepperProps {
  value: number
  onChange: (value: number) => void
  min?: number
  max?: number
  disabled?: boolean
  className?: string
}

export function QuantityStepper({
  value,
  onChange,
  min = 1,
  max = 999,
  disabled,
  className,
}: QuantityStepperProps) {
  const clamp = (next: number) => Math.min(max, Math.max(min, next))

  return (
    <div className={cn("inline-flex h-9 items-center rounded-md border border-input", className)}>
      <Button
        type="button"
        variant="ghost"
        size="icon-sm"
        className="rounded-r-none"
        disabled={disabled || value <= min}
        onClick={() => onChange(clamp(value - 1))}
        aria-label="Giảm số lượng"
      >
        <MinusIcon aria-hidden="true" />
      </Button>
      <input
        type="number"
        inputMode="numeric"
        value={value}
        min={min}
        max={max}
        disabled={disabled}
        onChange={(e) => {
          const next = Number(e.target.value)
          if (!Number.isNaN(next)) onChange(clamp(next))
        }}
        aria-label="Số lượng"
        className="tabular h-full w-12 border-x border-input bg-transparent text-center text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:opacity-50 [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
      />
      <Button
        type="button"
        variant="ghost"
        size="icon-sm"
        className="rounded-l-none"
        disabled={disabled || value >= max}
        onClick={() => onChange(clamp(value + 1))}
        aria-label="Tăng số lượng"
      >
        <PlusIcon aria-hidden="true" />
      </Button>
    </div>
  )
}
