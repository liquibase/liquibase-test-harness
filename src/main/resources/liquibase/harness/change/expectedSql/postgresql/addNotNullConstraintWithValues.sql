CREATE TABLE public.lb45 (name CHAR(20))
INSERT INTO public.lb45 (name) VALUES ('marcel')
ALTER TABLE public.lb45 ADD "columnWithInitialValue" BIGINT
UPDATE public.lb45 SET "columnWithInitialValue" = 0
ALTER TABLE public.lb45 ALTER COLUMN  "columnWithInitialValue" SET NOT NULL