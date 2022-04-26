CREATE TABLE public.authors_data AS SELECT DISTINCT email AS authors_email FROM public.authors WHERE email IS NOT NULL
ALTER TABLE public.authors_data ALTER COLUMN  authors_email SET NOT NULL
ALTER TABLE public.authors_data ADD PRIMARY KEY (authors_email)
ALTER TABLE public.authors ADD CONSTRAINT FK_AUTHORS_AUTHORS_DATA FOREIGN KEY (email) REFERENCES public.authors_data (authors_email)