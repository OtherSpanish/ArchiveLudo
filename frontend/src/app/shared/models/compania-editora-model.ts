/* Cuerpo de POST /api/editoras y PUT /api/editoras/{id}. */
export interface EditoraNueva {
  nombre: string;
  pais: string;
}

/* Compañía editora (GET /api/editoras). */
export interface CompaniaEditora {
  idEditora: number;
  nombre: string;
  pais: string;
}
