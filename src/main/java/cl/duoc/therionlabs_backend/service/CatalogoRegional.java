package cl.duoc.therionlabs_backend.service;

import java.util.Set;

final class CatalogoRegional {
    private CatalogoRegional() {}

    static final Set<String> comunas =
            Set.of(
                    "Algarrobo",
                    "Cabildo",
                    "Calera",
                    "Calle Larga",
                    "Cartagena",
                    "Casablanca",
                    "Catemu",
                    "Concón",
                    "El Quisco",
                    "El Tabo",
                    "Hijuelas",
                    "Isla de Pascua",
                    "Juan Fernández",
                    "La Cruz",
                    "La Ligua",
                    "Limache",
                    "Llaillay",
                    "Los Andes",
                    "Nogales",
                    "Olmué",
                    "Panquehue",
                    "Papudo",
                    "Petorca",
                    "Puchuncaví",
                    "Putaendo",
                    "Quillota",
                    "Quilpué",
                    "Quintero",
                    "Rinconada",
                    "San Antonio",
                    "San Esteban",
                    "San Felipe",
                    "Santa María",
                    "Santo Domingo",
                    "Valparaíso",
                    "Villa Alemana",
                    "Viña del Mar",
                    "Zapallar");
    static final Set<String> valparaiso =
            Set.of(
                    "Ingeniería en Marketing Digital",
                    "Ingeniería en Gestión Logística",
                    "Ingeniería en Comercio Exterior",
                    "Técnico en Administración",
                    "Técnico en Gestión Logística",
                    "Ingeniería en Informática",
                    "Analista Programador",
                    "Técnico Modelador Proyectista",
                    "Ingeniería en Construcción",
                    "Ingeniería en Prevención de Riesgos",
                    "Técnico en Construcción",
                    "Técnico Topógrafo Geomático",
                    "Gastronomía Internacional",
                    "Técnico en Electricidad y Automatización Industrial",
                    "Técnico en Mantenimiento Industrial",
                    "Ingeniería en Maquinaria y Vehículos Pesados",
                    "Ingeniería en Mantenimiento Industrial",
                    "Ingeniería en Electricidad y Automatización Industrial",
                    "Ingeniería Agrícola",
                    "Ingeniería en Mecánica Automotriz y Autotrónica",
                    "Técnico Veterinario y Pecuario",
                    "Técnico en Operación y Supervisión de Procesos Mineros",
                    "Técnico en Mecánica Automotriz y Autotrónica",
                    "Técnico en Maquinaria y Vehículos Pesados",
                    "Técnico en Laboratorio Clínico, Banco de Sangre e Imagenología",
                    "Informática Biomédica",
                    "Técnico en Enfermería",
                    "Técnico en Química y Farmacia",
                    "Técnico en Odontología",
                    "Administración en Turismo y Hospitalidad");
    static final Set<String> vina =
            Set.of(
                    "Ingeniería en Marketing Digital",
                    "Ingeniería en Gestión Logística",
                    "Ingeniería en Comercio Exterior",
                    "Técnico en Administración",
                    "Técnico en Gestión Logística",
                    "Ingeniería en Informática",
                    "Analista Programador",
                    "Contabilidad General Mención Legislación Tributaria",
                    "Auditoría",
                    "Comercio Exterior",
                    "Ingeniería en Administración Mención Gestión de Personas",
                    "Ingeniería en Administración Mención Innovación y Emprendimiento",
                    "Ingeniería en Administración Mención Finanzas",
                    "Comunicación Audiovisual",
                    "Relaciones Públicas y Comunicación Organizacional",
                    "Publicidad",
                    "Animación Digital",
                    "Ingeniería en Sonido",
                    "Tecnología en Sonido e Iluminación",
                    "Ilustración para Contextos Globales",
                    "Diseño Industrial e Innovación en Productos",
                    "Diseño Gráfico",
                    "Diseño de Vestuario",
                    "Diseño de Ambientes",
                    "Ingeniería en Redes y Telecomunicaciones",
                    "Preparador Físico");

    static Set<String> carreras(String sede) {
        return switch (sede == null ? "" : sede) {
            case "Valparaíso" -> valparaiso;
            case "Viña del Mar" -> vina;
            default -> Set.of();
        };
    }
}
