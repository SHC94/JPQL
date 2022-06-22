package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        //엔티티 매니저 팩토리는 하나만 생성해서 애플리케이션 전체에서 공유
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        //엔티티 매니저는 쓰레드간에 공유X (사용하고 버려야 한다).
        EntityManager em = emf.createEntityManager();

        //JPA의 모든 데이터 변경은 트랜잭션 안에서 실행
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("신형철");
            member.setAge(29);
            em.persist(member);

            em.flush();
            em.clear();

//            //TypedQuery : 반환 타입이 명확한 경우
//            TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
//            TypedQuery<String> query2 = em.createQuery("select m.username from Member m", String.class);
//
//            //Query : 반환 타입이 명확하지 않는 경우
//            Query query3 = em.createQuery("select m.username, m.age from Member m", Member.class);

            System.out.println("결과 조회 =====================================================");
            //결과 조회
            //query.getResultList() : 결과가 하나 이상일 때
            //결과가 있으면 리스트 반환, 결과가 없으면 빈 리스트 반환
            TypedQuery<Member> result1 = em.createQuery("select m from Member m", Member.class);
            for (Member member1 : result1.getResultList()) {
                System.out.println("member1 = " + member1);
            }

            //query.getSingleResult() : 결과가 정확히 하나, 단일 객체 반환
            //결과가 없으면 : javax.persistence.NoResultException
            //결과가 둘 이상이면 : javax.persistence.NonUniqueResultException
            //별로인듯?
            TypedQuery<Member> result2 = em.createQuery("select m from Member m", Member.class);
            result2.getSingleResult();
            System.out.println("result2 = " + result2);

            System.out.println("파라미터 바인딩 =====================================================");
            //파라미터 바인딩
            TypedQuery<Member> binding = em.createQuery("select m from Member m where m.username = :username", Member.class);
            binding.setParameter("username", "신형철");
            for (Member member1 : binding.getResultList()) {
                System.out.println("binding = " + member1.getUsername() + " / " + member1.getAge());
            }

            List<Member> bindingList = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "신형철")
                    .getResultList();
            for (Member member1 : bindingList) {
                System.out.println("binding = " + member1.getUsername() + " / " + member1.getAge());
            }

            //??????왜 쿼리1,2,3 붙이면 insert select 안되는거지???

            em.flush();
            em.clear();

            System.out.println("프로젝션 =====================================================");
            //프로젝션 : select 절에 조회할 대상을 지정하는 것.
            System.out.println("엔티티 프로젝션 =====================================================");
            //엔티티 프로젝션
            List<Member> projection = em.createQuery("select m from Member m", Member.class)
                    .getResultList();

            Member findMember = projection.get(0);
            findMember.setAge(19);
            System.out.println("findMember.getAge = " + findMember.getAge());

            System.out.println("임베디드 타입 프로젝션 =====================================================");
            //임베디드 타입 프로젝션
            List<Address> projection2 = em.createQuery("select o.address from Order o", Address.class)
                    .getResultList();


            System.out.println("스칼라 타입 프로젝션 =====================================================");
            List<Member> projection3 = em.createQuery("SELECT m.username, m.age FROM Member m")
                    .getResultList();

            Object o = projection3.get(0);
            Object[] result = (Object[]) o;
            System.out.println("result = " + result[0]);
            System.out.println("result = " + result[1]);


            em.createQuery("SELECT new jpql.MemberDTO(m.username, m.age) FROM Member m", MemberDTO.class)
                    .getResultList();

            System.out.println("트랜잭션 커밋 =====================================================");


            em.flush();
            em.clear();

            //페이징 API
            List<Member> paging = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();

            System.out.println("paging.size = " + paging.size());
            for(Member member1 : paging) {
                System.out.println("paging = " + member1.toString());
            }


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            emf.close();
        }

    }//end main()
}//end class()
