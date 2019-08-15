package br.com.alura.owasp.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import br.com.alura.owasp.model.Usuario;
import br.com.alura.owasp.util.ConnectionFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Repository
public class UsuarioDaoImpl implements UsuarioDao {


	@PersistenceContext
	private EntityManager manager;


	public void salva(Usuario usuario) {
		transformaASenhaDoUsuarioEmHash(usuario);
		manager.persist(usuario);
	}

	private void transformaASenhaDoUsuarioEmHash(Usuario usuario) {
		String salto = BCrypt.gensalt();
		String senhaHashed = BCrypt.hashpw(usuario.getSenha(), salto);
		usuario.setSenha(senhaHashed);
	}

	public Usuario procuraUsuario(Usuario usuario) {
		final TypedQuery<Usuario> query = manager.createQuery("select u from Usuario u where u.email=:email", Usuario.class);
		query.setParameter("email", usuario.getEmail());

		Usuario usuarioRetornado = query.getResultList().stream().findFirst().orElse(null);

		final boolean usuarioEncontrado = validaASenhaDoUsuarioComOHashDoBanco(usuario, usuarioRetornado);

		if(usuarioEncontrado) {
			return usuarioRetornado;
		}

		return null;
	}

	private boolean validaASenhaDoUsuarioComOHashDoBanco(Usuario usuario, Usuario usuarioRetornado) {
		if(usuarioRetornado == null) {
			return false;
		}

		return BCrypt.checkpw(usuario.getSenha(), usuarioRetornado.getSenha());

	}
}
