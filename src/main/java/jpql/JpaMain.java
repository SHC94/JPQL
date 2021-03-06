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
            Team team = new Team();
            team.setName("신형철");
            em.persist(team);

            Member member = new Member();
            member.setUsername("신형철");
            member.setAge(29);
            member.setTeam(team);
            member.setType(MemberType.ADMIN);
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

            em.flush();
            em.clear();

            System.out.println("페이징 =====================================================");
            //페이징 API
            List<Member> paging = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();

            System.out.println("paging.size = " + paging.size());
            for(Member member1 : paging) {
                System.out.println("paging = " + member1.toString());
            }

            System.out.println("조인 =====================================================");
            //inner 조인
            List<Member> join1 = em.createQuery("select m from Member m inner join m.team t", Member.class)
                    .getResultList();

            //outer 조인
            List<Member> join2 = em.createQuery("select m from Member m left outer join m.team t", Member.class)
                    .getResultList();

            //세타 조인
            List<Member> join3 = em.createQuery("select m from Member m, Team t where m.username = t.name", Member.class)
                    .getResultList();

            //on
            List<Member> join4 = em.createQuery("select m from Member m left join m.team t on t.name = '신형철'", Member.class)
                    .getResultList();

            //연관관계 없는 엔티티 외부 조인
//            List<Member> join5 = em.createQuery("select m from Member m join Team t on m.username = t.name'", Member.class)
//                    .getResultList();

            System.out.println("JPQL 타입표현과 기타식 =====================================================");
            List<Object[]> type = em.createQuery("SELECT m.username, 'HELLO', TRUE FROM Member m where m.type = jpql.MemberType.ADMIN")
                    .getResultList();


            System.out.println("조건식 =====================================================");
            //조건식 case 식
            String query ="select " +
                            "case when m.age <= 10 then '학생요금' " +
                            "when m.age >= 60 then '경로요금' " +
                            "else '일반 요금' " +
                            "end " +
                            "from Member m";
            List<String> condition1 = em.createQuery(query, String.class).getResultList();

            //COALESCE
            String query2 = "select coalesce(m.username, '이름 없는 회원') from Member m";
            List<String> condition2 = em.createQuery(query2, String.class).getResultList();

            //NULLIF
            String query3 = "select nullif(m.username = '신형철') from Member m";
            List<String> condition3 = em.createQuery(query2, String.class).getResultList();


            System.out.println("JPQL 기본 함수 =====================================================");
            String functionQuery1 = "select 'a' || 'b' from Member m";
            List<String> function1 = em.createQuery(functionQuery1, String.class).getResultList();

            //size
            String functionQuery2 = "select size(t.members) from Team t";
            List<Integer> function2 = em.createQuery(functionQuery2, Integer.class).getResultList();


            System.out.println("페치 조인 =====================================================");
            //페치 조인
            String fetchQuery1 = "select m from Member m join fetch m.team";
            List<Member> fetch1 = em.createQuery(fetchQuery1, Member.class).getResultList();

            String fetchQuery2 = "select t from Team t join fetch t.members";
            List<Team> fetch2 = em.createQuery(fetchQuery2, Team.class).getResultList();
            for(Team team2 : fetch2) {
                System.out.println("teamname = " + team.getName() + ", team = " + team);
                for (Member member2 : team.getMembers()) {
                    //페치 조인으로 팀과 회원을 함께 조회해서 지연 로딩 발생 안함
                    System.out.println("username = " + member2.getUsername() + " , member = " + member2);
                }//end for()
            }//end for()

            System.out.println("트랜잭션 커밋 =====================================================");
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            emf.close();
        }

    }//end main()
}//end class()
