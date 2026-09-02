import { MatDialogConfig } from '@angular/material/dialog';

/** Mismo criterio que SHELL_MOBILE_BREAKPOINT y @media (max-width: 767.98px) en styles.scss. */
export const MOBILE_BREAKPOINT = '(max-width: 767.98px)';

export const FORM_DIALOG_PANEL_CLASS = 'app-form-dialog';
export const FORM_DIALOG_DESKTOP_WIDTH = '480px';
export const FORM_DIALOG_MOBILE_WIDTH = '95vw';

export function isMobileViewport(): boolean {
  return (
    typeof globalThis.matchMedia !== 'undefined' &&
    globalThis.matchMedia(MOBILE_BREAKPOINT).matches
  );
}

/** Configuración MatDialog para formularios de alta/edición (almacén, zona, estantería). */
export function formDialogConfig(overrides: MatDialogConfig = {}): MatDialogConfig {
  const mobile = isMobileViewport();
  return {
    width: mobile ? FORM_DIALOG_MOBILE_WIDTH : FORM_DIALOG_DESKTOP_WIDTH,
    maxWidth: mobile ? FORM_DIALOG_MOBILE_WIDTH : FORM_DIALOG_DESKTOP_WIDTH,
    minWidth: mobile ? '0' : undefined,
    panelClass: FORM_DIALOG_PANEL_CLASS,
    ...overrides,
  };
}
