interface PageHeaderProps {
  eyebrow?: string;
  title: string;
  description?: string;
}

export function PageHeader({ eyebrow, title, description }: PageHeaderProps) {
  return (
    <div className="mb-8">
      {eyebrow && (
        <p className="mb-2 text-sm font-semibold uppercase tracking-[0.2em] text-[#38BDF8]">{eyebrow}</p>
      )}
      <h1 className="font-display text-3xl font-bold text-[#FFFFFF] md:text-4xl">{title}</h1>
      {description && <p className="mt-3 max-w-2xl text-[#A1A1AA]">{description}</p>}
    </div>
  );
}
