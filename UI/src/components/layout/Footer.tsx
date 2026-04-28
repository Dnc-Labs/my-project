export function Footer() {
  return (
    <footer className="border-t border-border bg-background">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-3 px-4 py-6 text-sm text-muted-foreground md:flex-row md:px-8">
        <p>© {new Date().getFullYear()} Marketplace. Built with Spring Boot + React.</p>
        <p className="font-mono text-xs">v0.1.0</p>
      </div>
    </footer>
  )
}
