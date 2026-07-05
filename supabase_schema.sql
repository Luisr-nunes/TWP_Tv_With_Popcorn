-- Execute este script no SQL Editor do Supabase para criar as tabelas necessárias

-- Tabela para armazenar as séries e filmes que o usuário acompanha
CREATE TABLE public.user_shows (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    tmdb_id TEXT NOT NULL,
    title TEXT NOT NULL,
    media_type TEXT NOT NULL,
    poster_path TEXT,
    progress INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    UNIQUE(user_id, tmdb_id)
);

-- Ativar segurança a nível de linha (RLS)
ALTER TABLE public.user_shows ENABLE ROW LEVEL SECURITY;

-- Política: Usuários só podem ver e editar seus próprios dados
CREATE POLICY "Users can only access their own shows"
ON public.user_shows
FOR ALL
USING (auth.uid() = user_id);
